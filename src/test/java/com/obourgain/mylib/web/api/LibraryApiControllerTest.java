package com.obourgain.mylib.web.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.ReadingService;
import com.obourgain.mylib.util.auth.ApiSecurity;
import com.obourgain.mylib.util.auth.WebSecurity;
import com.obourgain.mylib.util.auth.WebUser;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = LibraryApiController.class, properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret"})
@Import({ApiSecurity.class, WebSecurity.class})
class LibraryApiControllerTest {
    @Autowired MockMvc mvc;
    @MockBean BookService books;
    @MockBean ReadingService readings;
    @MockBean JwtDecoder decoder;

    @Test
    void requiresBearerAuthentication() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/readings").contentType("application/json")
                .content("{\"bookId\":1,\"date\":\"2026-11-17\"}"))
                .andExpect(status().isUnauthorized());
        var session = new org.springframework.mock.web.MockHttpSession();
        var context = new org.springframework.security.core.context.SecurityContextImpl(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                        .authenticated("session-user", "", List.of()));
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        mvc.perform(get("/api/books").session(session)).andExpect(status().isUnauthorized());
        when(decoder.decode("invalid")).thenThrow(new BadJwtException("Invalid token"));
        mvc.perform(get("/api/books").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(books, readings);
    }

    @Test
    void acceptsValidatedBearerToken() throws Exception {
        var token = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("valid")
                .header("alg", "RS256").subject("google-user").build();
        when(decoder.decode("valid")).thenReturn(token);
        when(books.findByUserId(eq("google-user"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book())));
        mvc.perform(get("/api/books").header("Authorization", "Bearer valid"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(7));
        verify(decoder).decode("valid");
    }

    @Test
    void listsOnlyAuthenticatedUsersBooks() throws Exception {
        Book book = book();
        when(books.findByUserId(eq("google-user"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book), PageRequest.of(0, 100), 1));
        mvc.perform(get("/api/books").with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Dune"))
                .andExpect(jsonPath("$.content[0].userId").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
        verify(books).findByUserId(eq("google-user"), any(PageRequest.class));
    }

    @Test
    void searchesOwnedBooksWithPaginationAndTags() throws Exception {
        Book book = book();
        Tag tag = new Tag();
        tag.setId(2L);
        tag.setText("Science fiction");
        book.setTags(java.util.Set.of(tag));
        var page = PageRequest.of(1, 2, org.springframework.data.domain.Sort.by("id"));
        when(books.searchBooks("author:Herbert", page, "google-user"))
                .thenReturn(new PageImpl<>(List.of(book), page, 3));
        mvc.perform(get("/api/books/search").param("q", "author:Herbert").param("page", "1").param("size", "2")
                        .with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Dune"))
                .andExpect(jsonPath("$.content[0].tags[0].text").value("Science fiction"))
                .andExpect(jsonPath("$.page").value(1)).andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));
        verify(books).searchBooks("author:Herbert", page, "google-user");
    }

    @Test
    void validatesSearchRequests() throws Exception {
        mvc.perform(get("/api/books/search").param("q", "Dune")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/books/search").with(jwt())).andExpect(status().isBadRequest());
        for (String query : List.of("", " ", "x".repeat(1001))) {
            mvc.perform(get("/api/books/search").param("q", query).with(jwt())).andExpect(status().isBadRequest());
        }
        for (String query : List.of("page=-1", "size=0", "size=101")) {
            mvc.perform(get("/api/books/search?" + query).param("q", "Dune").with(jwt()))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(books);
    }

    @Test
    void handlesEmptyAndInvalidSearchResults() throws Exception {
        var page = PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("id"));
        when(books.searchBooks("unknown", page, "google-user"))
                .thenReturn(org.springframework.data.domain.Page.empty(page));
        mvc.perform(get("/api/books/search").param("q", "unknown").with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
        when(books.searchBooks("title:", page, "google-user"))
                .thenThrow(new com.obourgain.mylib.util.search.SearchUnavailableException("parse",
                        new org.apache.lucene.queryparser.classic.ParseException("parse")));
        mvc.perform(get("/api/books/search").param("q", "title:").with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Invalid search query"));
        when(books.searchBooks("Dune", page, "google-user"))
                .thenThrow(new com.obourgain.mylib.util.search.SearchUnavailableException(new java.io.IOException()));
        mvc.perform(get("/api/books/search").param("q", "Dune").with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void detailAndOwnershipFailures() throws Exception {
        when(books.findOwnedBook("google-user", 7)).thenReturn(Optional.of(book()));
        mvc.perform(get("/api/books/7").with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
        when(books.findOwnedBook("google-user", 8)).thenReturn(Optional.empty());
        mvc.perform(get("/api/books/8").with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Book not found"));
        mvc.perform(post("/api/readings").with(jwt().jwt(j -> j.subject("google-user")))
                .contentType("application/json").content("{\"bookId\":8,\"date\":\"2026-11-17\"}"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(readings);
    }

    @Test
    void includesTagsInBookListAndDetailWithoutBackReferences() throws Exception {
        Book book = book();
        Tag tag = new Tag();
        tag.setId(2L);
        tag.setText("Science fiction");
        tag.setColor("#ffffff");
        tag.setBackgroundColor("#000000");
        tag.setBorderColor("#333333");
        tag.setPriority(10);
        tag.setUserId("google-user");
        org.springframework.test.util.ReflectionTestUtils.setField(tag, "books", java.util.Set.of(book));
        Tag second = new Tag();
        second.setId(3L);
        second.setText("Favorites");
        book.setTags(java.util.Set.of(second, tag));
        when(books.findByUserId(eq("google-user"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(books.findOwnedBook("google-user", 7)).thenReturn(Optional.of(book));

        for (String path : List.of("/api/books", "/api/books/7")) {
            String prefix = path.equals("/api/books") ? "$.content[0]" : "$";
            mvc.perform(get(path).with(jwt().jwt(j -> j.subject("google-user"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(prefix + ".tags.length()").value(2))
                    .andExpect(jsonPath(prefix + ".tags[0].id").value(2))
                    .andExpect(jsonPath(prefix + ".tags[0].text").value("Science fiction"))
                    .andExpect(jsonPath(prefix + ".tags[0].color").value("#ffffff"))
                    .andExpect(jsonPath(prefix + ".tags[0].backgroundColor").value("#000000"))
                    .andExpect(jsonPath(prefix + ".tags[0].borderColor").value("#333333"))
                    .andExpect(jsonPath(prefix + ".tags[0].priority").value(10))
                    .andExpect(jsonPath(prefix + ".tags[0].books").doesNotExist())
                    .andExpect(jsonPath(prefix + ".tags[0].userId").doesNotExist())
                    .andExpect(jsonPath(prefix + ".tags[1].id").value(3));
        }
    }

    @Test
    void returnsEmptyTagArraysForUntaggedBooks() throws Exception {
        Book book = book();
        for (boolean initialized : List.of(false, true)) {
            if (initialized) book.setTags(java.util.Set.of());
            when(books.findByUserId(eq("google-user"), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(book)));
            when(books.findOwnedBook("google-user", 7)).thenReturn(Optional.of(book));
            mvc.perform(get("/api/books").with(jwt().jwt(j -> j.subject("google-user"))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].tags").isEmpty());
            mvc.perform(get("/api/books/7").with(jwt().jwt(j -> j.subject("google-user"))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.tags").isEmpty());
        }
    }

    @Test
    void createsBookWithServerControlledIdentity() throws Exception {
        doAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            WebUser user = invocation.getArgument(1);
            org.junit.jupiter.api.Assertions.assertNull(book.getId());
            org.junit.jupiter.api.Assertions.assertEquals("google-user", user.getId());
            book.setId(7L);
            return null;
        }).when(books).createOrUpdateBook(any(), any(), eq(java.util.Set.of()));
        mvc.perform(post("/api/books").with(jwt().jwt(j -> j.subject("google-user")))
                .contentType("application/json").content("{\"title\":\"Dune\",\"pages\":412,\"id\":99,\"userId\":\"other\"}"))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/books/7"))
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void createsAndListsReadingsWithoutRecursiveEntities() throws Exception {
        Book book = book();
        Reading reading = new Reading();
        reading.setId(3L);
        reading.setBook(book);
        reading.setDate(LocalDate.of(2026, 11, 17));
        reading.setYear(2026);
        reading.setMonth(11);
        when(books.findOwnedBook("google-user", 7)).thenReturn(Optional.of(book));
        when(readings.save(argThat(u -> u.getId().equals("google-user")), eq(book), eq(reading.getDate())))
                .thenReturn(reading);
        mvc.perform(post("/api/readings").with(jwt().jwt(j -> j.subject("google-user")))
                .contentType("application/json").content("{\"bookId\":7,\"date\":\"2026-11-17\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.bookId").value(7))
                .andExpect(jsonPath("$.date").value("2026-11-17"));
        when(readings.findByUserId(eq("google-user"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(reading)));
        mvc.perform(get("/api/readings").with(jwt().jwt(j -> j.subject("google-user"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].bookId").value(7))
                .andExpect(jsonPath("$.content[0].book").doesNotExist());
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        for (String body : List.of("{}", "{\"title\":\" \"}", "{\"title\":\"Dune\",\"pages\":-1}", "{\"title\":\"Dune\",\"status\":\"invalid\"}")) {
            mvc.perform(post("/api/books").with(jwt()).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }
        for (String body : List.of("{}", "{\"bookId\":0,\"date\":\"2026-11-17\"}", "{\"bookId\":7,\"date\":\"invalid\"}")) {
            mvc.perform(post("/api/readings").with(jwt()).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }
        for (String query : List.of("page=-1", "size=0", "size=101", "page=invalid")) {
            mvc.perform(get("/api/books?" + query).with(jwt())).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(books, readings);
    }

    @Test
    void webLoginAndCsrfRemainEnabled() throws Exception {
        mvc.perform(get("/booklist").accept("text/html")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/reading").with(oidcLogin())).andExpect(status().isForbidden());
    }

    private Book book() {
        Book book = new Book();
        book.setId(7L);
        book.setTitle("Dune");
        book.setUserId("google-user");
        return book;
    }
}

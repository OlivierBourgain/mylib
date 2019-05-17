import React from 'react';
import {Link} from "react-router-dom";

function Pagination(props) {
    const page = props.page;
    const nbPages = props.nbPages;

    if (nbPages <= 1) return null;

    var start = 0;
    var end = nbPages;

    if (nbPages <= 8) ; // Nothing to do, we display all pages
    else if (page <= 4) end = 8; // Display pages 1 to 8
    else if (page >= nbPages - 5) start = nbPages - 8; // Display pages nbPages-8 to nbPages
    else {
        start = page - 3;
        end = page + 4;
    }

    return <ul className="pagination pagination-sm">
        <li className="page-item" key="page-start">
            <Link className="page-link" to="/books?page=1" onClick={() => props.updatePage(0)}>&laquo;</Link>
        </li>
        {start > 0 && <li className="page-item disabled" key="page-break-start">
            <span className="page-link">...</span>
        </li>}
        {
            [...Array(end - start).keys()].map(i =>
            <li className={"page-item " + (start + i === page ? 'active':'')} key={`page-${start + i}`}>
                <Link className="page-link" to={`/books?page=${start + i}`} onClick={() => props.updatePage(start + i)}>
                    {start + i + 1}
                </Link>
            </li>
        )}
        {end < nbPages - 1 && <li className="page-item disabled" key="page-break-end">
                <span className="page-link">...</span>
            </li>
        }
        <li className="page-item" key="page-end">
            <Link className="page-link" to={`/books?page=${nbPages}`} onClick={() => props.updatePage(nbPages - 1)}>&raquo;</Link>
        </li>
    </ul>;
}

export default Pagination;
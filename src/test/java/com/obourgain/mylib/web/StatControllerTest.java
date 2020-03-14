package com.obourgain.mylib.web;

import com.obourgain.mylib.service.StatService;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StatControllerTest {

    @Test
    public void toHighChartJsWithEmptyList() {
        StatController fixture = new StatController();
        String res= fixture.toHighChartJs(Lists.emptyList());
        assertEquals("{}", res);
    }


    @Test
    public void toHighChartJs() {
        StatController fixture = new StatController();
        List<StatService.StatData> datas = new ArrayList<>();
        datas.add(new StatService.StatData("k1", 1));
        datas.add(new StatService.StatData("k2", 2));
        datas.add(new StatService.StatData("k3", 3));
        String res= fixture.toHighChartJs(datas);
        assertEquals("""
                    {
                        "labels": ["k1","k2","k3"],
                        "datasets": [{
                            "data": [1,2,3],
                            "backgroundColor": "rgb(255, 255, 255)",
                            "borderWidth": 0
                        }]
                    }
                    """
                , res);
    }
}
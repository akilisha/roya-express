package com.akilisha.oss.web.shared.view;

import com.akilisha.oss.web.core.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HandlebarsViewTest {

    @Test
    @Disabled("temp disable")
    void render() {
        Response res = mock(Response.class);
        SampleTodo todo = new SampleTodo("wake up", false, LocalDate.now());

        HandlebarsView handlebarsView = new HandlebarsView("src/test/resources/templates/handlebars");
        handlebarsView.render("sample", todo, (err, data) -> res.send(data));

        verify(res).send("<h2>wake up</h2>\n" +
                "<small>Created on 2025-05-12</small>\n" +
                "<p></p>");
    }
}

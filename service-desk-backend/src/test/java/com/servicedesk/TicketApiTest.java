package com.servicedesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.seed-demo=false")
@AutoConfigureMockMvc
class TicketApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    private long createTicket(String title, String priority) throws Exception {
        String body = """
                {"title":"%s","description":"Something is broken","requesterName":"Test User",
                 "requesterEmail":"test@example.com","priority":"%s","category":"SOFTWARE"}
                """.formatted(title, priority);
        MvcResult result = mvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void update(long id, String json) throws Exception {
        mvc.perform(patch("/api/tickets/" + id).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    void createsTicketWithSlaDeadline() throws Exception {
        long id = createTicket("Cannot log in", "URGENT");
        mvc.perform(get("/api/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("SD-" + id))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.dueAt").exists())
                .andExpect(jsonPath("$.overdue").value(false))
                .andExpect(jsonPath("$.comments.length()").value(0));
    }

    @Test
    void rejectsInvalidTicket() throws Exception {
        mvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"requesterEmail\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void updatesStatusPriorityAndAssignee() throws Exception {
        long id = createTicket("Slow wifi", "LOW");
        mvc.perform(patch("/api/tickets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\",\"priority\":\"HIGH\",\"assignee\":\"Alex\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assignee").value("Alex"));

        mvc.perform(get("/api/tickets").param("status", "IN_PROGRESS").param("q", "wifi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        update(id, "{\"assignee\":\"\"}");
        mvc.perform(get("/api/tickets/" + id))
                .andExpect(jsonPath("$.assignee").doesNotExist());
    }

    @Test
    void resolvingSetsResolvedTimeAndReopeningClearsIt() throws Exception {
        long id = createTicket("Monitor flickers", "MEDIUM");
        update(id, "{\"status\":\"RESOLVED\"}");
        mvc.perform(get("/api/tickets/" + id)).andExpect(jsonPath("$.resolvedAt").exists());
        update(id, "{\"status\":\"OPEN\"}");
        mvc.perform(get("/api/tickets/" + id)).andExpect(jsonPath("$.resolvedAt").doesNotExist());
    }

    @Test
    void recordsActivityLog() throws Exception {
        long id = createTicket("Audit trail", "MEDIUM");
        update(id, "{\"status\":\"IN_PROGRESS\",\"assignee\":\"Sam\"}");
        mvc.perform(post("/api/tickets/" + id + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"author\":\"Sam\",\"message\":\"On it\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/tickets/" + id + "/activity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].type").value("CREATED"))
                .andExpect(jsonPath("$[1].type").value("STATUS_CHANGED"))
                .andExpect(jsonPath("$[2].type").value("ASSIGNED"))
                .andExpect(jsonPath("$[3].type").value("COMMENTED"));
    }

    @Test
    void addsComments() throws Exception {
        long id = createTicket("Keyboard keys stuck", "LOW");
        mvc.perform(post("/api/tickets/" + id + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"author\":\"Alex\",\"message\":\"Looking into it\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("Alex"));

        mvc.perform(get("/api/tickets/" + id))
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].message").value("Looking into it"));
    }

    @Test
    void deletesTicket() throws Exception {
        long id = createTicket("Duplicate ticket", "LOW");
        mvc.perform(delete("/api/tickets/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/tickets/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void agentsListShowsOpenTicketCount() throws Exception {
        mvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Robin\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"robin\"}"))
                .andExpect(status().isConflict());

        long id = createTicket("Needs Robin", "HIGH");
        update(id, "{\"assignee\":\"Robin\"}");

        mvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Robin')].openTickets").value(1));
    }

    @Test
    void statsCountTickets() throws Exception {
        createTicket("Stats ticket", "LOW");
        mvc.perform(get("/api/tickets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.byStatus.OPEN").isNumber())
                .andExpect(jsonPath("$.overdue").isNumber());
    }
}

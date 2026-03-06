package id.ac.ui.cs.advprog.yomu.social.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClanController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClanService clanService;

    private ObjectMapper objectMapper;
    private Clan dummyClan;

    // String constants/variables
    private String clanId;
    private String leaderId;
    private String memberId;
    private String clanName;
    private String joinSuccessMsg;
    private String leaveSuccessMsg;
    private String deleteSuccessMsg;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Initialize variables
        clanId = "clan-123";
        leaderId = "user-456";
        memberId = "user-789";
        clanName = "Wibu Elite";
        joinSuccessMsg = "Berhasil bergabung";
        leaveSuccessMsg = "Berhasil keluar dari clan";
        deleteSuccessMsg = "Clan berhasil dihapus";

        dummyClan = new Clan();
        dummyClan.setId(clanId);
        dummyClan.setName(clanName);
        dummyClan.setLeaderUserId(leaderId);
    }

    @Test
    void testCreateClanSuccess() throws Exception {
        ClanRequest request = new ClanRequest();
        request.setName(clanName);
        request.setUserId(leaderId);

        when(clanService.createClan(any(ClanRequest.class))).thenReturn(dummyClan);

        mockMvc.perform(post("/api/clans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(clanName))
                .andExpect(jsonPath("$.id").value(clanId));

        verify(clanService, times(1)).createClan(any(ClanRequest.class));
    }

    @Test
    void testGetAllClans() throws Exception {
        List<Clan> clans = Arrays.asList(dummyClan);
        when(clanService.findAll()).thenReturn(clans);

        mockMvc.perform(get("/api/clans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(clanName));

        verify(clanService, times(1)).findAll();
    }

    @Test
    void testJoinClan() throws Exception {
        mockMvc.perform(post("/api/clans/" + clanId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(memberId))
                .andExpect(status().isOk())
                .andExpect(content().string(joinSuccessMsg));

        verify(clanService, times(1)).joinClan(eq(clanId), eq(memberId));
    }

    @Test
    void testLeaveClan() throws Exception {
        mockMvc.perform(post("/api/clans/" + clanId + "/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(leaderId))
                .andExpect(status().isOk())
                .andExpect(content().string(leaveSuccessMsg));

        verify(clanService, times(1)).leaveClan(eq(clanId), eq(leaderId));
    }

    @Test
    void testDeleteClan() throws Exception {
        mockMvc.perform(post("/api/clans/" + clanId + "/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(leaderId))
                .andExpect(status().isOk())
                .andExpect(content().string(deleteSuccessMsg));

        verify(clanService, times(1)).deleteClan(eq(clanId), eq(leaderId));
    }
}
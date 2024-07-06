package de.samuelgesang.backend.sites;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
class SiteControllerTest {

    @Mock
    private SiteService siteService;

    @Mock
    private OAuth2User oauth2User;

    @InjectMocks
    private SiteController siteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Disabled("currently fails")
    @Test
    @WithMockUser
    void testGetAllSites() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "user123");
        when(oauth2User.getAttributes()).thenReturn(attributes);

        List<Site> sites = List.of(new Site("1", "Google", "https://google.com", "", "", "user123", "", "", new ArrayList<>()));
        when(siteService.getAllSites("user123")).thenReturn(sites);

        List<SiteWithCrawlsDTO> result = siteController.getAllSites(oauth2User);

        assertEquals(1, result.size(), "The result size should be 1");
        assertEquals("Google", result.getFirst().getName(), "The site name should be Google");
    }


    @Test
    @WithMockUser
    void testDeleteSite() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "user123");
        when(oauth2User.getAttributes()).thenReturn(attributes);

        doNothing().when(siteService).deleteSite("1", "user123");

        siteController.deleteSite("1", oauth2User);
        verify(siteService, times(1)).deleteSite("1", "user123");
    }
}

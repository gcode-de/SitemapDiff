package de.samuelgesang.backend.sites;

import org.junit.jupiter.api.BeforeEach;
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

    @Test
    @WithMockUser
    void testGetAllSites() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "user123");
        when(oauth2User.getAttributes()).thenReturn(attributes);

        List<Site> sites = List.of(new Site("1", "Google", "https://google.com", "", "user123", "string", new ArrayList<>()));
        when(siteService.getAllSites("user123")).thenReturn(sites);

        List<Site> result = siteController.getAllSites(oauth2User);
        assertEquals(1, result.size());
        assertEquals("Google", result.get(0).getName());
    }

    @Test
    @WithMockUser
    void testCreateSite() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "user123");
        when(oauth2User.getAttributes()).thenReturn(attributes);

        Site site = new Site("1", "Google", "https://google.com", "", "user123", "string", new ArrayList<>());
        when(siteService.createSite(any(Site.class))).thenReturn(site);

        Site result = siteController.createSite(site, oauth2User);
        assertEquals("Google", result.getName());
    }

    @Test
    @WithMockUser
    void testUpdateSite() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "user123");
        when(oauth2User.getAttributes()).thenReturn(attributes);

        Site site = new Site("1", "Google", "https://google.com", "", "user123", "string", new ArrayList<>());
        when(siteService.updateSite(eq("1"), any(Site.class))).thenReturn(site);

        Site result = siteController.updateSite("1", site, oauth2User);
        assertEquals("Google", result.getName());
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

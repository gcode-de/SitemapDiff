package de.samuelgesang.backend.sitemaps;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SitemapController.class)
class SitemapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SitemapService sitemapService;

    @Test
    void testFindSitemaps() throws Exception {
        String baseURL = "http://example.com";
        String[] sitemaps = {"http://example.com/sitemap1.xml", "http://example.com/sitemap2.xml"};

        Mockito.when(sitemapService.findSitemaps(baseURL)).thenReturn(sitemaps);

        mockMvc.perform(get("/api/sitemaps/find")
                        .param("baseURL", baseURL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(sitemaps[0]))
                .andExpect(jsonPath("$[1]").value(sitemaps[1]));
    }

    @Test
    void testFindSitemapsWithNoResults() throws Exception {
        String baseURL = "http://example.com";
        String[] sitemaps = {};

        Mockito.when(sitemapService.findSitemaps(baseURL)).thenReturn(sitemaps);

        mockMvc.perform(get("/api/sitemaps/find")
                        .param("baseURL", baseURL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testFindSitemapsWithMissingParam() throws Exception {
        mockMvc.perform(get("/api/sitemaps/find"))
                .andExpect(status().isBadRequest());
    }
}

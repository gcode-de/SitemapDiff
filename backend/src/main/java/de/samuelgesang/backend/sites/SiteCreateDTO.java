package de.samuelgesang.backend.sites;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SiteCreateDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String baseURL;

    @NotBlank
    private String sitemap;

    //    @NotNull
    private String userId;

    private String crawlSchedule;
    private String email;
}

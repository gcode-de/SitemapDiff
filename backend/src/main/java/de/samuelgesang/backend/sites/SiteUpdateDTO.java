package de.samuelgesang.backend.sites;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SiteUpdateDTO {
    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String baseURL;

    @NotBlank
    private String sitemap;

    @NotNull
    private String userId;

    private String crawlSchedule;
    private String email;
}

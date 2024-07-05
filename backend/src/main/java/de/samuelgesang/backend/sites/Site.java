package de.samuelgesang.backend.sites;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@Document(collection = "sites")
@NoArgsConstructor
@AllArgsConstructor
public class Site {

    @Id
    private String id;
    private String name;
    private String baseURL;
    private String sitemap;
    private String favicon;
    private String userId;
    private String crawlSchedule;
    private String email;
    private List<String> crawlIds;
}

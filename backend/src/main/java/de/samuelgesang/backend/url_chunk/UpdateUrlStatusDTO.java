package de.samuelgesang.backend.url_chunk;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateUrlStatusDTO {
    private String url;
    private boolean checked;
}

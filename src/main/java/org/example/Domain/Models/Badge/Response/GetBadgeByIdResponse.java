package org.example.Domain.Models.Badge.Response;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class GetBadgeByIdResponse {
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private String name;

    public GetBadgeByIdResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public GetBadgeByIdResponse() {
    }


}


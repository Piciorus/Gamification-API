package org.example.Domain.Models.Badge.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class GetAllBadgesResponse {
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private String name;

    public GetAllBadgesResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public GetAllBadgesResponse() {
    }


}

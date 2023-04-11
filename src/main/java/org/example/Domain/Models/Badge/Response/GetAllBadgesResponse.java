package org.example.Domain.Models.Badge.Response;

import lombok.Getter;
import lombok.Setter;

public class GetAllBadgesResponse {
    @Getter @Setter private int id;

    @Getter @Setter private String name;

    public GetAllBadgesResponse(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public GetAllBadgesResponse() {
    }


}

package org.example.Domain.Models.Badge.Response;


import lombok.Getter;
import lombok.Setter;

public class GetBadgeByIdResponse {
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String name;

    public GetBadgeByIdResponse(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public GetBadgeByIdResponse() {
    }


}


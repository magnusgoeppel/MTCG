package org.mtcg.app.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData
{
    private String name;
    private String bio;
    private String image;

    public UserData() {}
}
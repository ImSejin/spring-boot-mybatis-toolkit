package io.github.imsejin.template.webapp.author.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Author {

    private long id;
    private String name;
    private String country;
    private LocalDate birthdate;

}

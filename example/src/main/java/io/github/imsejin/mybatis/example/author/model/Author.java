package io.github.imsejin.mybatis.example.author.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

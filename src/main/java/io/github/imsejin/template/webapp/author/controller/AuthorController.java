package io.github.imsejin.template.webapp.author.controller;

import io.github.imsejin.template.webapp.author.model.Author;
import io.github.imsejin.template.webapp.author.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public Object getAuthors() {
        return authorService.getAuthors();
    }

    @GetMapping("id/{id}")
    public Author getAuthor(@PathVariable long id) {
        return authorService.getAuthor(id);
    }

    @GetMapping("name/{name}")
    public Author getAuthor(@PathVariable String name) {
        return authorService.getAuthor(name);
    }

    @PostMapping
    public void addAuthors(@RequestBody Author... authors) {
        if (authors.length > 1) {
            authorService.addAuthors(authors);
        } else {
            authorService.addAuthor(authors[0]);
        }
    }

    @PutMapping
    public void changeAuthor(@RequestBody Author author) {
        authorService.changeAuthor(author);
    }

    @DeleteMapping("id/{id}")
    public void removeAuthor(@PathVariable long id) {
        Author author = new Author();
        author.setId(id);

        authorService.removeAuthor(author);
    }

}

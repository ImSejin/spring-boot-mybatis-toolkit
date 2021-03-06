package io.github.imsejin.mybatis.example.author.controller;

import io.github.imsejin.mybatis.example.author.model.Author;
import io.github.imsejin.mybatis.example.author.service.AuthorService;
import io.github.imsejin.mybatis.pagination.model.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public Object getAuthors(PageRequest request) {
        return authorService.getAuthors(request);
    }

    @GetMapping("id/{id}")
    public Object getAuthor(@PathVariable long id) {
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

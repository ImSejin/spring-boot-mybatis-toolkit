package io.github.imsejin.mybatis.example.author.service;

import io.github.imsejin.mybatis.example.author.mapper.AuthorMapper;
import io.github.imsejin.mybatis.example.author.model.Author;
import io.github.imsejin.mybatis.pagination.model.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorMapper authorMapper;

    public List<Author> getAuthors(PageRequest request) {
        return authorMapper.selectAll(request);
    }

    public Author getAuthor(long id) {
        return authorMapper.selectById(id);
    }

    public Author getAuthor(String name) {
        return authorMapper.selectByName(name);
    }

    public void addAuthor(Author author) {
        authorMapper.insert(author);
    }

    public void addAuthors(Author... authors) {
        authorMapper.insertAll(authors);
    }

    public void changeAuthor(Author author) {
        authorMapper.update(author);
    }

    public void removeAuthor(Author author) {
        authorMapper.delete(author);
    }

}

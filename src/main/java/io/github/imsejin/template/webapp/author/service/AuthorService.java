package io.github.imsejin.template.webapp.author.service;

import io.github.imsejin.template.webapp.author.mapper.AuthorMapper;
import io.github.imsejin.template.webapp.author.model.Author;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorMapper authorMapper;

    public List<Author> getAuthors() {
        return authorMapper.selectAll(new Page(36, 1, 10));
    }

    public Author getAuthor(long id) {
//        return authorMapper.selectById(id, 1);
        return authorMapper.selectById(id, new Page(36, 1, 9)).get(0);
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

package com.lrj.his.mdm.web;

import com.lrj.his.common.web.Result;
import com.lrj.his.mdm.domain.Dictionary;
import com.lrj.his.mdm.service.DictionaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mdm/dicts")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/{type}")
    public Result<List<Dictionary>> listByType(@PathVariable("type") String type) {
        return Result.ok(dictionaryService.listByType(type));
    }
}

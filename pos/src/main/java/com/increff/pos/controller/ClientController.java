package com.increff.pos.controller;

import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.response.ClientResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientDto dto;

    @GetMapping
    public List<ClientResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Use paginated version if pagination parameters are provided
        // For backward compatibility, return all when default parameters are used
        return dto.getAll(page, size);
    }

    @GetMapping("/search")
    public List<ClientResponse> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Use paginated version if pagination parameters are provided
//        if (page > 0 || size != 10) {
//            return dto.searchByNamePaginated(name, page, size);
//        }
        // For backward compatibility, return all when default parameters are used
        return dto.searchByName(name, page, size);
    }

    @PostMapping
    public ClientResponse add(@Valid @RequestBody ClientForm form) {
        return dto.add(form);
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable @Min(1) Integer id, @Valid @RequestBody ClientForm form) {
        return dto.update(id, form);
    }
}

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
    private ClientDto clientDto;

    @GetMapping
    public List<ClientResponse> getAll() {

        return clientDto.getAll();
    }

    @GetMapping("/search")
    public List<ClientResponse> searchByName(@RequestParam String name) {

        return clientDto.searchByName(name);
    }

    @PostMapping
    public ClientResponse add(@Valid @RequestBody ClientForm form) {

        return clientDto.add(form);
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable @Min(1) Integer id, @Valid @RequestBody ClientForm form) {

        return clientDto.update(id, form);
    }
}

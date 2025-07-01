package com.increff.pos.controller;

import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.response.ClientResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Api
@RestController
@RequestMapping("/clients")
@CrossOrigin
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    private static final Logger logger = Logger.getLogger(ClientController.class);

    @ApiOperation(value = "Fetches list of all clients")
    @GetMapping
    //TODO: to remove response entity across all controllers
    public List<ClientResponse> getAll() {

        logger.info("Fetching all clients");

        return clientDto.getAll();
    }

    @ApiOperation(value = "Search clients by name")
    @GetMapping("/search")
    public List<ClientResponse> searchByName(@RequestParam String name) {

        logger.info("Searching clients by name: " + name);

        return clientDto.searchByName(name);
    }

    @ApiOperation(value = "Adds new client")
    @PostMapping
    public ClientResponse add(@Valid @RequestBody ClientForm form) {

        logger.info("Adding new client");

        return clientDto.add(form);
    }

    @ApiOperation(value = "Updates existing client")
    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable @Min(1) Integer id, @Valid @RequestBody ClientForm form) {

        logger.info("Updating client with id: " + id);

        return clientDto.update(id, form);
    }
}

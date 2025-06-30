package com.increff.pos.controller;

import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.response.ClientResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ClientResponse>> getAll() {

        logger.info("Fetching all clients");

        List<ClientResponse> clients = clientDto.getAll();
        return ResponseEntity.ok(clients);
    }

    @ApiOperation(value = "Search clients by name")
    @GetMapping("/search")
    public ResponseEntity<List<ClientResponse>> searchByName(@RequestParam String name) {

        logger.info("Searching clients by name: " + name);

        List<ClientResponse> clients = clientDto.searchByName(name);
        return ResponseEntity.ok(clients);
    }

    @ApiOperation(value = "Adds new client")
    @PostMapping
    public ResponseEntity<ClientResponse> add(@Valid @RequestBody ClientForm form) {

        logger.info("Adding new client");

        ClientResponse client = clientDto.add(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @ApiOperation(value = "Updates existing client")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable @Min(1) Integer id, @Valid @RequestBody ClientForm form) {

        logger.info("Updating client with id: " + id);

        ClientResponse client = clientDto.update(id, form);
        return ResponseEntity.ok(client);
    }
}

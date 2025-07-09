package com.increff.pos.util;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConvertUtil {

    @Autowired
    private ModelMapper modelMapper;
// id -> not id
    /**
     * Convert single object from source to destination type
     */
    public <S, D> D convert(S source, Class<D> destinationType) {
        if (source == null) {
            return null;
        }
        return modelMapper.map(source, destinationType);
    }

    /**
     * Convert list of objects from source to destination type
     */
    public <S, D> List<D> convertList(List<S> sourceList, Class<D> destinationType) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(source -> convert(source, destinationType))
                .collect(Collectors.toList());
    }

    /**
     * Convert with custom configuration
     */
    public <S, D> D convertWithConfig(S source, Class<D> destinationType,
                                      java.util.function.Consumer<ModelMapper> config) {
        if (source == null) {
            return null;
        }

        ModelMapper customMapper = new ModelMapper();
        config.accept(customMapper);
        return customMapper.map(source, destinationType);
    }
}
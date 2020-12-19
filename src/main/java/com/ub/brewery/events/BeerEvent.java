package com.ub.brewery.events;

import com.ub.brewery.model.BeerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BeerEvent implements Serializable {
    private static final long serialVersionUID = -1968701294745972508L;

    private BeerDto beerDto;
}

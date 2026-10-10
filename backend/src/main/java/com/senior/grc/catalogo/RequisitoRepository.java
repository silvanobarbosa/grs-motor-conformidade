package com.senior.grc.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RequisitoRepository extends JpaRepository<RequisitoRegulatorio, UUID> {

    List<RequisitoRegulatorio> findByNormaCodigo(String normaCodigo);

    List<RequisitoRegulatorio> findByNormaCodigoAndItem(String normaCodigo, String item);
}

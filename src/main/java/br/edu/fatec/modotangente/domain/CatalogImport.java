package br.edu.fatec.modotangente.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "catalog_imports")
public class CatalogImport {
    @Id
    @Column(name = "source", length = 500, nullable = false)
    private String source;
    @Column(name = "sha256", length = 64, nullable = false)
    private String sha256;

    protected CatalogImport() { }
    public CatalogImport(String source, String sha256) {
        this.source = source;
        this.sha256 = sha256;
    }

    public String getSha256() { return sha256; }
}

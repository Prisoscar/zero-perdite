package mkle.zeroperdite.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "client")
public class Client extends AbstractEntityNoVersion {

    @NotNull(message = "cognome cannot be null")
    @Size(max = 100, message = "cognome cannot contain more than 100 characters")
    private String cognome;

    @Size(max = 100, message = "nome cannot contain more than 100 characters")
    private String nome;

    @Size(max = 100, message = "città cannot contain more than 100 characters")
    private String citta;

//    @NotNull(message = "codice fiscale cannot be null")
    @Size(min = 7, max = 16, message = "codice fiscale should contain between 7 and 16 characters")
    private String codiceFiscale;

    @Size(max = 200, message = "indirizzo cannot contain more than 200 characters")
    private String indirizzo;

    @Size(max = 30, message = "telefono cannot contain more than 30 characters")
    private String telefono;

    @Email(message = "mail must be a valid email address")
    @Size(max = 200, message = "mail cannot contain more than 200 characters")
    private String mail;

    public String getCognome() {
        return cognome;
    }

    public Client setCognome(String cognome) {
        this.cognome = cognome;
        return this;
    }

    public String getNome() {
        return nome;
    }

    public Client setNome(String nome) {
        this.nome = nome;
        return this;
    }

    public String getCitta() {
        return citta;
    }

    public Client setCitta(String citta) {
        this.citta = citta;
        return this;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public Client setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
        return this;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public Client setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
        return this;
    }

    public String getTelefono() {
        return telefono;
    }

    public Client setTelefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public String getMail() {
        return mail;
    }

    public Client setMail(String mail) {
        this.mail = mail;
        return this;
    }
}

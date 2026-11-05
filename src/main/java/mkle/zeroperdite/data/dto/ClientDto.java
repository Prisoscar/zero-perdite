package mkle.zeroperdite.data.dto;

import mkle.zeroperdite.data.Client;

public record ClientDto(String id, String cognome, String nome, String citta,
                        String codiceFiscale, String indirizzo, String telefono, String mail) {

    public Client toClient() {
        return new Client()
                .<Client>setId(id)
                .setCognome(cognome)
                .setNome(nome)
                .setCitta(citta)
                .setCodiceFiscale(codiceFiscale)
                .setIndirizzo(indirizzo)
                .setTelefono(telefono)
                .setMail(mail);
    }
}

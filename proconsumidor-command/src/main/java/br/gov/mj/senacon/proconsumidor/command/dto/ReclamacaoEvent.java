package br.gov.mj.senacon.proconsumidor.command.dto;

import java.time.LocalDateTime;

public class ReclamacaoEvent {

    private String protocolo;
    private String fornecedorId;
    private String status;
    private LocalDateTime dataEvento;
    private String originIp;
    private String usuarioBot;

    // Getters and Setters
    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
    public String getFornecedorId() { return fornecedorId; }
    public void setFornecedorId(String fornecedorId) { this.fornecedorId = fornecedorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
    public String getOriginIp() { return originIp; }
    public void setOriginIp(String originIp) { this.originIp = originIp; }
    public String getUsuarioBot() { return usuarioBot; }
    public void setUsuarioBot(String usuarioBot) { this.usuarioBot = usuarioBot; }
}

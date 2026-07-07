package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.BaAktivasi;
import com.vnet.vnet_backend.entity.CustomerRegistration;
import com.vnet.vnet_backend.entity.RegistrationStatus;
import com.vnet.vnet_backend.repository.BaAktivasiRepository;
import com.vnet.vnet_backend.repository.CustomerRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BaAktivasiService {

    private final BaAktivasiRepository repository;
    private final CustomerRegistrationRepository registrationRepository;
    private final CustomerRegistrationService registrationService;

    public Optional<BaAktivasi> getBaByRegistrationId(Long regId) {
        return repository.findByRegistrationId(regId);
    }

    @Transactional
    public BaAktivasi saveBa(Long regId, BaAktivasi inputData) {
        CustomerRegistration reg = registrationRepository.findById(regId)
                .orElseThrow(() -> new RuntimeException("Registrasi tidak ditemukan!"));

        BaAktivasi ba = repository.findByRegistrationId(regId)
                .orElseGet(() -> BaAktivasi.builder().registration(reg).build());

        // Update fields
        ba.setNamaTeknisi(inputData.getNamaTeknisi());
        ba.setCatatanTeknisi(inputData.getCatatanTeknisi());

        // Item 1: ONT
        ba.setQtyOnt(inputData.getQtyOnt());
        ba.setSnOnt(inputData.getSnOnt());
        if (inputData.getFotoOnt() != null) {
            ba.setFotoOnt(inputData.getFotoOnt());
        }

        // Item 2: Router
        ba.setQtyRouter(inputData.getQtyRouter());
        ba.setSnRouter(inputData.getSnRouter());
        if (inputData.getFotoRouter() != null) {
            ba.setFotoRouter(inputData.getFotoRouter());
        }

        // Item 3: Kabel
        ba.setQtyKabel(inputData.getQtyKabel());
        ba.setSnKabel(inputData.getSnKabel());
        if (inputData.getFotoKabel() != null) {
            ba.setFotoKabel(inputData.getFotoKabel());
        }

        // Item 4: Roset
        ba.setQtyRoset(inputData.getQtyRoset());
        ba.setSnRoset(inputData.getSnRoset());
        if (inputData.getFotoRoset() != null) {
            ba.setFotoRoset(inputData.getFotoRoset());
        }

        // Item 5: Aksesoris
        ba.setQtyAksesoris(inputData.getQtyAksesoris());
        ba.setSnAksesoris(inputData.getSnAksesoris());
        if (inputData.getFotoAksesoris() != null) {
            ba.setFotoAksesoris(inputData.getFotoAksesoris());
        }

        if (inputData.getTandaTanganCustomer() != null) {
            ba.setTandaTanganCustomer(inputData.getTandaTanganCustomer());
        }
        if (inputData.getTandaTanganTeknisi() != null) {
            ba.setTandaTanganTeknisi(inputData.getTandaTanganTeknisi());
        }

        ba.setBandwidth(inputData.getBandwidth());
        ba.setPanjangKabel(inputData.getPanjangKabel());
        ba.setKoordinatRumah(inputData.getKoordinatRumah());
        ba.setKodeOdp(inputData.getKodeOdp());
        ba.setPortOdp(inputData.getPortOdp());
        ba.setPortOdpTerpakai(inputData.getPortOdpTerpakai());
        ba.setPop(inputData.getPop());
        ba.setOlt(inputData.getOlt());
        ba.setRosset(inputData.getRosset());
        ba.setPigtail(inputData.getPigtail());
        ba.setPatchcore(inputData.getPatchcore());
        ba.setSplicing(inputData.getSplicing());
        ba.setRedamanOutputKabel(inputData.getRedamanOutputKabel());
        ba.setJumlahBracket(inputData.getJumlahBracket());
        ba.setJumlahSClamp(inputData.getJumlahSClamp());
        ba.setStatusAktivasi(inputData.getStatusAktivasi());

        if (inputData.getFotoRumah() != null) {
            ba.setFotoRumah(inputData.getFotoRumah());
        }
        if (inputData.getFotoOdpDepan() != null) {
            ba.setFotoOdpDepan(inputData.getFotoOdpDepan());
        }
        if (inputData.getFotoRedamanOdp() != null) {
            ba.setFotoRedamanOdp(inputData.getFotoRedamanOdp());
        }
        if (inputData.getFotoDalemOdp() != null) {
            ba.setFotoDalemOdp(inputData.getFotoDalemOdp());
        }
        if (inputData.getFotoRedamanKabel() != null) {
            ba.setFotoRedamanKabel(inputData.getFotoRedamanKabel());
        }
        if (inputData.getFotoDepanOnt() != null) {
            ba.setFotoDepanOnt(inputData.getFotoDepanOnt());
        }
        if (inputData.getFotoBelakangOnt() != null) {
            ba.setFotoBelakangOnt(inputData.getFotoBelakangOnt());
        }
        if (inputData.getFotoSpeedTest() != null) {
            ba.setFotoSpeedTest(inputData.getFotoSpeedTest());
        }

        BaAktivasi saved = repository.save(ba);

        // Transition registration status to SELESAI (which auto-triggers Customer creation!)
        if (reg.getStatus() != RegistrationStatus.SELESAI) {
            registrationService.updateStatus(regId, RegistrationStatus.SELESAI, null);
        }

        return saved;
    }
}

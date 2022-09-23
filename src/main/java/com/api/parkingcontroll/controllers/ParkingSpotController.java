package com.api.parkingcontroll.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontroll.dtos.ParkingSpotDto;
import com.api.parkingcontroll.models.ParkingSpotModel;
import com.api.parkingcontroll.services.ParkingSpotServeces;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {
  
  final ParkingSpotServeces parkingSpotServeces;

  public ParkingSpotController(ParkingSpotServeces parkingSpotServeces) {
    this.parkingSpotServeces = parkingSpotServeces;
  }

  @PostMapping
  public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
    if (parkingSpotServeces.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
    }
    if (parkingSpotServeces.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
    }
    if (parkingSpotServeces.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already registered for this apartment/block!");
    }
    var ParkingSpotModel = new ParkingSpotModel();
    BeanUtils.copyProperties(parkingSpotDto, ParkingSpotModel);
    ParkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
    return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotServeces.save(ParkingSpotModel));
  }

  @GetMapping
  public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpot(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.status(HttpStatus.OK).body(parkingSpotServeces.findAll(pageable));
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<Object> getById(@PathVariable(value = "id") UUID id) {
    Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotServeces.findById(id);
    if (parkingSpotModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
    }
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id) {
    Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotServeces.findById(id);
    if(parkingSpotModelOptional.isPresent()) {
      parkingSpotServeces.delete(parkingSpotModelOptional.get());
      return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
    }
  }
  
  @PutMapping("/{id}")
  public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto) {
    Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotServeces.findById(id);
    if (parkingSpotModelOptional.isPresent()) {
      var parkingSpotModel = new ParkingSpotModel();
      BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
      parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
      parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
      return ResponseEntity.status(HttpStatus.OK).body(parkingSpotServeces.save(parkingSpotModel));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
    }
  }
  
  
}

package com.example.api.service;

import com.example.api.dto.request.SavedPlaceRequest;
import com.example.api.dto.request.UpdateProfileRequest;
import com.example.api.dto.response.CustomerProfileResponse;
import com.example.api.dto.response.SavedPlaceResponse;
import com.example.api.entity.CustomerProfile;
import com.example.api.entity.SavedPlace;
import com.example.api.entity.User;
import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.jpa.CustomerProfileRepository;
import com.example.api.repository.jpa.SavedPlaceRepository;
import com.example.api.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final UserRepository userRepository;


    public CustomerProfileResponse getProfile(Long userId) {

        CustomerProfile profile = customerProfileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        CustomerProfileResponse reponse = CustomerProfileResponse.builder()
                .userId(userId)
                .fullName(profile.getFullName())
                .avatarUrl(profile.getAvatarUrl())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .createdAt(profile.getCreatedAt())
                .build();

        return reponse;
    }

    @Transactional
    public CustomerProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {

        CustomerProfile profile = customerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                    return CustomerProfile.builder()
                            .id(userId)
                            .user(user)
                            .build();
                });

        profile.setFullName(request.getFullName());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());

        CustomerProfile save = customerProfileRepository.save(profile);

        return mapToResponse(save);
    }

    @Transactional
    public SavedPlaceResponse createSavedPlace(Long userId, SavedPlaceRequest request) {

        CustomerProfile profile = customerProfileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        if(request.isDefault()){
            savedPlaceRepository.resetDefaultByUserId(userId);
        }

        SavedPlace place = SavedPlace.builder()
                .customerProfile(profile)
                .label((request.getLabel()))
                .addressText(request.getAddressText())
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        SavedPlace saved = savedPlaceRepository.save(place);

        return mapToSavedPlaceResponse(saved);

    }

    public List<SavedPlaceResponse> getSavedPlaces(Long userId) {
        List<SavedPlace> places = savedPlaceRepository.findByCustomerProfileId(userId);

        return places.stream()
                .map(this::mapToSavedPlaceResponse)
                .toList();
    }

    public SavedPlaceResponse updateSavedPlace(Long userId, Long placeId, SavedPlaceRequest request) {

        SavedPlace place = savedPlaceRepository.findById(placeId)
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.getCustomerProfile().getId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (request.isDefault()) {
            savedPlaceRepository.resetDefaultByUserId(userId);
        }
        place.setLabel(request.getLabel());
        place.setAddressText(request.getAddressText());
        place.setWard((request.getWard()));
        place.setDistrict(request.getDistrict());
        place.setCity(request.getCity());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setIsDefault(request.isDefault());

        savedPlaceRepository.save(place);

        return mapToSavedPlaceResponse(place);
    }

    @Transactional
    public void deleteSavedPlace(Long userId, Long placeId) {

        SavedPlace place =  savedPlaceRepository.findById(placeId)
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        if(!place.getCustomerProfile().getId().equals(userId)){
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        place.softDelete();
        savedPlaceRepository.save(place);

    }

    private SavedPlaceResponse mapToSavedPlaceResponse(SavedPlace place) {
        return SavedPlaceResponse.builder()
                .id(place.getId())
                .label(place.getLabel())
                .addressText(place.getAddressText())
                .ward(place.getWard())
                .district(place.getDistrict())
                .city(place.getCity())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .isDefault(place.getIsDefault())
                .createdAt(place.getCreatedAt())
                .build();
    }

    private CustomerProfileResponse mapToResponse(CustomerProfile profile) {
        return CustomerProfileResponse.builder()
                .userId(profile.getId())
                .fullName(profile.getFullName())
                .avatarUrl(profile.getAvatarUrl())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .createdAt(profile.getCreatedAt())
                .build();
    }

}

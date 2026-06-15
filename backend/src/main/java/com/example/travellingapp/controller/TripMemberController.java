package com.example.travellingapp.controller;


import com.example.travellingapp.dto.request.AddTripMemberDTO;
import com.example.travellingapp.dto.request.update.UpdateTripMemberRoleDTO;
import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/trips/{tripId}/members")
public interface TripMemberController {
    @GetMapping
    ResponseEntity<ResponseBody<Object>> getTripMembers(@PathVariable Long tripId);

    @PostMapping
    ResponseEntity<ResponseBody<Object>> addTripMember(
            @PathVariable Long tripId,
            @RequestBody AddTripMemberDTO addTripMemberDTO);

    @PatchMapping("/{tripMemberId}/role")
    ResponseEntity<ResponseBody<Object>> updateTripMemberRole(
            @PathVariable Long tripId,
            @PathVariable Long tripMemberId,
            @RequestBody UpdateTripMemberRoleDTO updateTripMemberRoleDTO
    );

    @DeleteMapping("/{tripMemberId}")
    ResponseEntity<ResponseBody<Object>> removeTripMember(
            @PathVariable Long tripId,
            @PathVariable Long tripMemberId
    );
}

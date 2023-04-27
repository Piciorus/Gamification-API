package org.example.Service.Implementation;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Badge.Request.CreateBadgeRequest;
import org.example.Domain.Models.Badge.Response.GetAllBadgesResponse;
import org.example.Domain.Models.Badge.Response.GetBadgeByIdResponse;
import org.example.Repository.BadgesRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IBadgesService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BadgeService implements IBadgesService {
    private final BadgesRepository badgesRepository;
    private final UsersRepository usersRepository;
    private final Mapper mapper;

    public BadgeService(BadgesRepository badgesRepository, UsersRepository usersRepository, Mapper mapper) {
        this.badgesRepository = badgesRepository;
        this.usersRepository = usersRepository;
        this.mapper = mapper;
    }

    @Override
    public Badge createBadge(CreateBadgeRequest createBadgeRequest) {
        return badgesRepository.save(mapper.CreateBadgeToBadgeRequest(createBadgeRequest));
    }

    @Override
    public void deleteBadge(UUID idBadge) {
        badgesRepository.deleteById(idBadge);
    }

    @Override
    public GetBadgeByIdResponse findBadgeById(UUID idBadge) {
        Badge badge = badgesRepository.getById(idBadge);
        return mapper.BadgeToGetByIdBadgeResponse(badge);
    }

    @Override
    public Iterable<GetAllBadgesResponse> findAllBadges() {
        List<GetAllBadgesResponse> list = new ArrayList<>();
        badgesRepository.findAll().forEach(badge -> {
            list.add(mapper.BadgeToGetAllBadgesResponse(badge));
        });
        return list;
    }

    @Override
    public void rewardBadge(UUID idBadge, UUID idUser) {
        Badge badge = badgesRepository.getById(idBadge);
        User user = usersRepository.getById(idUser);
        user.getBadgesList().add(badge);
        badge.getUsers().add(user);
        badgesRepository.save(badge);
        usersRepository.save(user);
    }

    @Override
    public Iterable<GetBadgeByIdResponse> findBadgesByUserId(UUID idUser) {
        User user = usersRepository.getById(idUser);
        List<GetBadgeByIdResponse> list = new ArrayList<>();
        user.getBadgesList().forEach(badge -> {
            list.add(mapper.BadgeToGetByIdBadgeResponse(badge));
        });
        return list;
    }
}

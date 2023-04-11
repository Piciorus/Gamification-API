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
    public Badge updateBadge(Badge badges, Integer id) {
        Badge badgeFromDb = badgesRepository.getById(id);
        badgeFromDb.setName(badges.getName());
        return badgesRepository.save(badgeFromDb);
    }

    @Override
    public void deleteBadge(Integer id) {
        badgesRepository.deleteById(id);
    }

    @Override
    public GetBadgeByIdResponse findBadgeById(Integer id) {
        Badge badge = badgesRepository.getById(id);
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
    public void rewardBadge(int idBadge, int idUser) {
        Badge badge = badgesRepository.getById(idBadge);
        User user = usersRepository.getById(idUser);
        user.getBadgesList().add(badge);
        badge.getUsers().add(user);
        badgesRepository.save(badge);
        usersRepository.save(user);
    }

    @Override
    public Iterable<Badge> findBadgesByUserId(int idUser) {
        User user=usersRepository.getById(idUser);
        return user.getBadgesList();
    }
}

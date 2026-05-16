package id.ac.ui.cs.advprog.yomu.social.service;

import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonStatusResponse;

public interface SeasonService {
    SeasonStatusResponse getCurrentSeason();

    SeasonEndResponse endSeason();
}

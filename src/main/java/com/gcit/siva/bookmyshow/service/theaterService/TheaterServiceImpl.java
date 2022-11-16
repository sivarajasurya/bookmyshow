package com.gcit.siva.bookmyshow.service.theaterService;

import com.gcit.siva.bookmyshow.entity.Movie;
import com.gcit.siva.bookmyshow.entity.ShowScreen;
import com.gcit.siva.bookmyshow.entity.Theater;
import com.gcit.siva.bookmyshow.repository.ShowScreenRepo;
import com.gcit.siva.bookmyshow.repository.TheaterRepo;
import com.gcit.siva.bookmyshow.service.movieService.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TheaterServiceImpl implements TheaterService{

    @Autowired
    private TheaterRepo theaterRepo;

    @Autowired
    private ShowScreenRepo showScreenRepo;

    @Autowired
    private MovieService movieService;

    @Override
    public Theater saveTheaterName(Theater theater) {
        return theaterRepo.save(theater);
    }

    @Override
    public List<Theater> listAllTheaterName() {
        return theaterRepo.findAll();
    }

    @Override
    public Theater findByID(long id) {
        Optional<Theater> optionalTheater = theaterRepo.findById(id);
        if (optionalTheater.isPresent()){
            return optionalTheater.get();
        }throw new RuntimeException("Theater not found " + id);
    }

    @Override
    public Theater findTheaterByTheaterNames(String theaterName){
        return theaterRepo.findTheaterByTheaterName(theaterName);
    }

    @Override
    public List<Theater> findTheaterNameForMovieName(String movieName){
        Movie movie = movieService.findMovieByMovieName(movieName);
        long movieID = movie.getMovieId();
        List<ShowScreen> showScreens = showScreenRepo.findAllShowScreeByMovieID(movieID);
        long[] theaterIds = new long[showScreens.size()];
        for (int i = 0; i<showScreens.size();i++){
            theaterIds[i]=showScreens.get(i).getTheater().getTheaterId();
        }
        List<Theater> list = new ArrayList<>();

        for (int i = 0 ; i<showScreens.size(); i++){
            list.add(this.findByID(theaterIds[i]));
        }
        return list;
    }
    @Override
    public List<Theater> getAll() {
        return theaterRepo.findAllTheater();
    }


}


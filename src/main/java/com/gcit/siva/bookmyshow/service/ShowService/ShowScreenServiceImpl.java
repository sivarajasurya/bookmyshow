package com.gcit.siva.bookmyshow.service.ShowService;

import com.gcit.siva.bookmyshow.DTO.BookTicketDto.BookSeatForShowRequest;
import com.gcit.siva.bookmyshow.DTO.BookTicketDto.BookSeatForShowResponse;
import com.gcit.siva.bookmyshow.DTO.ListAllShowTimingsByTheaterNameOrMovieName.ListAllMoviesByTheaterName.AllMoviesByTheaterNameDto;
import com.gcit.siva.bookmyshow.DTO.ListAllShowTimingsByTheaterNameOrMovieName.ListAllMoviesByTheaterName.MovieNameDto;
import com.gcit.siva.bookmyshow.DTO.ListAllShowTimingsByTheaterNameOrMovieName.ListAllTheaterByMovieName.AllTheaterByMovieNameDto;
import com.gcit.siva.bookmyshow.DTO.ListAllShowTimingsByTheaterNameOrMovieName.ShowScreenTimingDto;
import com.gcit.siva.bookmyshow.DTO.SeatAvailableDto;
import com.gcit.siva.bookmyshow.entity.Movie;
import com.gcit.siva.bookmyshow.entity.ShowScreen;
import com.gcit.siva.bookmyshow.entity.Theater;
import com.gcit.siva.bookmyshow.repository.ShowScreenRepo;
import com.gcit.siva.bookmyshow.request.ShowRequest;
import com.gcit.siva.bookmyshow.service.movieService.MovieService;
import com.gcit.siva.bookmyshow.service.theaterService.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShowScreenServiceImpl implements ShowScreenService {

    @Autowired
    private ShowScreenRepo showScreenRepo;

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private MovieService movieService;

    long num;

    @Override
    public List<ShowScreen> findAllShowScreen() {
        return showScreenRepo.findAll();
    }

    @Override
    public ShowScreen saveShow(long theaterId, long movieID, ShowRequest showRequest) {

        ShowScreen showScreen = new ShowScreen();
        Theater theater1 = theaterService.findByID(theaterId);
        Movie movie1 = movieService.findMovieById(movieID);

        showScreen.setTheater(theater1);
        showScreen.setMovie(movie1);
        showScreen.setTotalSeat(showRequest.getTotalSeat());
        showScreen.setBookedSeat(showRequest.getBookedSeat());
        showScreen.setDate(showRequest.getDate());

        return showScreenRepo.save(showScreen);
    }

    @Override
    public SeatAvailableDto getTicketAvailabilityByScreenId(long id) {
        SeatAvailableDto seatAvailableDto = new SeatAvailableDto();

        Optional<ShowScreen> showScreen = Optional.of(new ShowScreen());
        showScreen = showScreenRepo.findById(id);

        Integer totalSeat = showScreen.get().getTotalSeat();
        Integer bookedSeat = showScreen.get().getBookedSeat();

        Theater theater = showScreen.get().getTheater();
        Movie movie = showScreen.get().getMovie();

        String theaterName = theater.getTheaterName();
        String movieName = movie.getMovieName();

        seatAvailableDto.setTheaterName(theaterName);
        seatAvailableDto.setMovieName(movieName);

        if ((totalSeat - bookedSeat) > 0) {
            seatAvailableDto.setAvailableSeats(totalSeat - bookedSeat);
            return seatAvailableDto;
        }
        seatAvailableDto.setAvailableSeats(0);
        return seatAvailableDto;
    }

    @Override
    public BookSeatForShowResponse bookSeatAvailableForShowScreen(BookSeatForShowRequest showRequest){
        long showId1 = showRequest.getShowId();
        Optional<ShowScreen> showScreen = showScreenRepo.findById(showId1);

        BookSeatForShowResponse ticket = new BookSeatForShowResponse();

        ticket.setTheaterName(showScreen.get().getTheater().getTheaterName());
        ticket.setMovieName(showScreen.get().getMovie().getMovieName());
        int availableNum = showScreen.get().getTotalSeat() - showScreen.get().getBookedSeat();
        if (showRequest.getNumOfTicket()<availableNum){
            int i = showScreen.get().getBookedSeat() + showRequest.getNumOfTicket();
            ticket.setAvailableSeats(availableNum-showRequest.getNumOfTicket());
            ticket.setBookedSeats(showRequest.getNumOfTicket());
            showScreenRepo.decreaseCountOfBookedSeat(i,showId1);
            ticket.setShowDateAndTiming(showScreen.get().getDate());
            ticket.setStatus("The Ticket has been booked");
        }else {
            ticket.setAvailableSeats(availableNum);
            ticket.setBookedSeats(0);
            ticket.setStatus("The Ticket has not been booked");
        }

        return ticket;
    }

    @Override
    public List<AllTheaterByMovieNameDto> findAllShowScreenByMovieName(String movieName) {

        Movie movie = movieService.findMovieByMovieName(movieName);
        long id = movie.getMovieId();

        List<Long> theaterId = showScreenRepo.findTheaterIdByMovieId(id);

        List<AllTheaterByMovieNameDto> list = new ArrayList<>();

        for (Long k : theaterId) {
            AllTheaterByMovieNameDto dto = new AllTheaterByMovieNameDto();
            dto.setMovieName(movie.getMovieName());
            Theater byID = theaterService.findByID(k);
            dto.setTheaterName(byID.getTheaterName());

            List<ShowScreen> allShowsByTheaterID = showScreenRepo.getAllShowsByTheaterID(k);
            List<ShowScreenTimingDto> showDto = new ArrayList<>();

            for (ShowScreen j : allShowsByTheaterID) {
                ShowScreenTimingDto dto1 = new ShowScreenTimingDto();
                dto1.setShowId(j.getShowId());
                dto1.setDate(j.getDate());
                dto1.setTotalSeat(j.getTotalSeat());
                dto1.setBookedSeat(j.getBookedSeat());
                showDto.add(dto1);
            }
            dto.setShowScreens(showDto);
            list.add(dto);
        }

        return list;
    }


    @Override
    public AllMoviesByTheaterNameDto findAllShowScreenByTheaterName(String theaterName) {

        Theater theater = theaterService.findTheaterByTheaterNames(theaterName);
        long id = theater.getTheaterId();

        List<Long> movieId = showScreenRepo.findMovieIdByTheaterId(id);

        AllMoviesByTheaterNameDto dto = new AllMoviesByTheaterNameDto();
        dto.setTheaterName(theater.getTheaterName());

        List<MovieNameDto> list1 = new ArrayList<>();
        for (Long k : movieId) {
            MovieNameDto dto1 = new MovieNameDto();
            dto1.setMovieName(movieService.findMovieById(k).getMovieName());
            List<ShowScreen> allShowsByMovieId = showScreenRepo.getAllShowsByTheaterID(id);
            List<ShowScreenTimingDto> list2 = new ArrayList<>();
            for (ShowScreen t : allShowsByMovieId) {
                ShowScreenTimingDto dto2 = new ShowScreenTimingDto();
                if (t.getMovie().getMovieId() == k){
                    dto2.setShowId(t.getShowId());
                    dto2.setDate(t.getDate());
                    dto2.setTotalSeat(t.getTotalSeat());
                    dto2.setBookedSeat(t.getBookedSeat());
                    list2.add(dto2);
                }
            }
            dto1.setScreenTiming(list2);
           list1.add(dto1);
        }
            dto.setMovieName(list1);
        return dto;
    }

}

rainfall <- read.csv("/Users/Thoughtworker/Programming/chicago_sewage_overflow/data/hourly_rainfall.csv")
rainfall_by_day <- aggregate(HPCP ~ as.Date(DATE) , data = rainfall[rainfall$HPCP != 99999,], sum)

ord_rain <- rainfall[rainfall$HPCP != 99999 & rainfall$STATION_NAME == "CHICAGO OHARE INTERNATIONAL AIRPORT IL US",c(6,7)]
ord_rain[1] <- data.frame(strptime(ord_rain[,1], format="%Y-%m-%d %H:%M:%S %z"))
ord_rain_by_day <- aggregate(HPCP ~ as.Date(DATE) , data = ord_rain, sum)
colnames(ord_rain_by_day)[1] <- "date"
mdw_rain <- rainfall[rainfall$HPCP != 99999 & rainfall$STATION_NAME == "CHICAGO MIDWAY AIRPORT 3 SW IL US",c(6,7)]
mdw_rain[1] <- data.frame(strptime(mdw_rain[,1], format="%Y-%m-%d %H:%M:%S %z"))
mdw_rain_by_day <- aggregate(HPCP ~ as.Date(DATE) , data = mdw_rain, sum)
colnames(mdw_rain_by_day)[1] <- "date"

cso_events <- read.csv("/Users/Thoughtworker/Programming/chicago_sewage_overflow/data/cso_events_scraped_12_15_2013.csv")
cso_events[4] <- data.frame(strptime(cso_events[,4], format="%Y-%m-%d %H:%M:%S %z"))
cso_events[3] <- data.frame(strptime(cso_events[,3], format="%Y-%m-%d %H:%M:%S %z"))
cso_events[5] <- data.frame(apply(cso_events[3],2,trunc.POSIXt, units = 'hours')) + 3600
colnames(cso_events)[3] <- "StartTime"
colnames(cso_events)[4] <- "EndTime"
colnames(cso_events)[5] <- "StartHour"
for(i in seq(6,55)) {
 cso_events[i] <- cso_events[i-1] - 3600
 colnames(cso_events)[i] <- paste("Prev",i - 5,"Hour",sep="")
}

ordRainByHour <- function(x) {
  rainfall <- 0
  if(sum(class(x) == "POSIXct") > 0) {
  	ordEntry <- ord_rain[which(ord_rain[,1] == x),2][1]
  	if(class(ordEntry) == 'integer' && !is.na(ordEntry)) {
	  rainfall <- ordEntry
	}
  }
  return(rainfall)
}

mdwdRainByHour <- function(x) {
  rainfall <- 0
  if(sum(class(x) == "POSIXct") > 0) {
  	mdwEntry <- mdw_rain[which(mdw_rain[,1] == x),2][1]
  	if(class(mdwEntry) == 'integer' && !is.na(mdwEntry)) {
	  rainfall <- mdwEntry
	}
  }
  return(rainfall)
}



truncHours <- function(x) {
  trunc.POSIXt(x, 'hours')
}
csoEventOccured <- function(x,cso_events) {
	truncX <- truncHours(x)
	occuredOnlyWithinHour <- any(truncHours(cso_events[,3]) == truncX) || any(truncHours(cso_events[,4]) == truncX)
	return(occuredOnlyWithinHour || any(truncHours(cso_events[,3]) <= truncX && truncHours(cso_events[,4]) >= truncHours(x)))
}
allHours <- data.frame(seq(min(cso_events[,55]), max(cso_events[,5]), 'hour'))
allHours[2] <- lapply(allHours[,1],csoEventOccured)



roundDown <- function(x) {
 H <- as.integer(format(x, "%H"))
 M <- as.integer(format(x, "%M"))
 S <- as.integer(format(x, "%S"))
 D <- format(x, "%Y-%m-%d")
 secs <- 3600*H + 60*M + S
 as.POSIXct(floor(secs/(3600))*(3600), origin=D)
}
roundUp <- function(x) {
 H <- as.integer(format(x, "%H"))
 M <- as.integer(format(x, "%M"))
 S <- as.integer(format(x, "%S"))
 D <- format(x, "%Y-%m-%d")
 secs <- 3600*H + 60*M + S
 as.POSIXct(ceiling(secs/(3600))*(3600), origin=D)
}

sep_cso <- split(cso_events, cso_events[1])

format(strptime("1970-01-01", "%Y-%m-%d", tz="UTC") + round(as.numeric(your.time)/900)*900,"%H:%M")


round.POSIXct <- function(x, units = c("mins", "5 mins", "10 mins", "15 
mins", "quarter hours", "30 mins", "half hours", "hours")){ 
        if(is.numeric(units)) units <- as.character(units) 
        units <- match.arg(units) 
        r <- switch(units, 
                "mins" = 60, 
                "5 mins" = 60*5, 
                "10 mins" = 60*10, 
                "15 mins"=, "quarter hours" = 60*15, 
                "30 mins"=, "half hours" = 60*30, 
                "hours" = 60*60 
        ) 
        H <- as.integer(format(x, "%H") 
        M <- as.integer(format(x, "%M")) 
        S <- as.integer(format(x, "%S")) 
        D <- format(x, "%Y-%m-%d") 
        secs <- 3600*H + 60*M + S 
        as.POSIXct(round(secs/r)*r, origin=D) 
}
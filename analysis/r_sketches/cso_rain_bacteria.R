wq <- read.csv('/Users/Thoughtworker/Programming/chicago-river-sewage/data/processed_data/clean-waterway-measurements.csv')
wq$collect.date. <- strptime(wq$collect.date., format='%Y-%m-%d %H:%M:%S')
wq$fec_col.cts.100ml. <- as.integer(paste(wq$fec_col.cts.100ml.))
wq$temp.deg_c. <- as.integer(paste(wq$temp.deg_c.))
wq$fec_col.cts.100ml.[is.na(wq$fec_col.cts.100ml.)] <- 0 # Should we replace with zero?  Need to check the original intent of empty cells

cso <- read.csv("/Users/Thoughtworker/Programming/chicago-river-sewage/data/processed_data/cso_events_timestamped.csv")
cso[4] <- data.frame(strptime(cso[,4], format="%Y-%m-%d %H:%M:%S %z"))
cso[3] <- data.frame(strptime(cso[,3], format="%Y-%m-%d %H:%M:%S %z"))
cso[5] <- cso[4] - cso[3]
colnames(cso)[3] <- "StartTime"
colnames(cso)[4] <- "EndTime"
colnames(cso)[5] <- "TotalOutflow"

rainfall <- read.csv("/Users/Thoughtworker/Programming/chicago-river-sewage/data/hourly_rainfall_2005-2012.csv")
mdw_rain <- rainfall[rainfall$HPCP != 99999 & rainfall$STATION_NAME == "CHICAGO MIDWAY AIRPORT 3 SW IL US",c(6,7)]
mdw_rain[1] <- data.frame(strptime(mdw_rain[,1], format="%Y%m%d %H:%M"))
mdwrain <- aggregate(HPCP ~ DATE , data = mdw_rain, sum)

outfall_locations <- c('DS-M49','DS-M45','DS-M41','DS-M40','DS-M38','DS-M30','DS-M35','DS-M25')

addPreviousWeekSums <- function(sampled_data, outfall_locations, cso) {
	for(j in outfall_locations){
		outflow <- subset(cso, cso$NBPS == j)
		week <- vector()
		weekIndex <- ncol(sampled_data) + 1
		for(i in seq(1,nrow(sampled_data))) {
			week <- append(week, as.integer(sum(outflow$TotalOutflow[which((outflow$StartTime < (sampled_data$collect.date.[i] + 24 * 60 * 60)) & (outflow$StartTime > (sampled_data$collect.date.[i] - 7 * 24 * 60 * 60)))])))
		}
		sampled_data[,weekIndex] <- week
		colnames(sampled_data)[weekIndex] <- paste(gsub("-","_",j), "csoSumPrevweek", sep="_")
	}
	return(sampled_data)
}
addPreviousDaysTotals <- function(sampled_data, outfall_locations, cso) {
	for(j in outfall_locations){
		outflow <- subset(cso, cso$NBPS == j)
		for(i in seq(0,7)){
			day <- vector()
			for(k in seq(1,nrow(sampled_data))) {
				day <- append(day, as.integer(sum(outflow$TotalOutflow[which((outflow$StartTime < (sampled_data$collect.date.[k] - (i-1) * 24 * 60 * 60)) & (outflow$StartTime > (sampled_data$collect.date.[k] - i  * 24 * 60 * 60)))])))
			}
			dayIndex <- ncol(sampled_data) + 1
			sampled_data[,dayIndex] <- day
			colnames(sampled_data)[dayIndex] <- paste(gsub("-","_",j), "csoSum", i, "DaysBack", sep="_")
		}
	}
	return(sampled_data)
}
addPreviousDaysTotalsAllLocations <- function(sampled_data, cso) {
	for(i in seq(0,3)){
		day <- vector()
		for(k in seq(1,nrow(sampled_data))) {
			matchingEvents <- which((cso$StartTime < (sampled_data$collect.date.[k] - (i-1) * 24 * 60 * 60)) & (cso$StartTime > (sampled_data$collect.date.[k] - i  * 24 * 60 * 60)))
			day <- append(day, as.integer(sum(cso$TotalOutflow[matchingEvents])))
		}
		dayIndex <- ncol(sampled_data) + 1
		sampled_data[,dayIndex] <- day
		colnames(sampled_data)[dayIndex] <- paste("csoSum", i, "DaysBack", sep="_")
	}
	return(sampled_data)
}
attachRainForPastWeek <- function(ss, mdwrain) {
	weekIndex <- ncol(ss) + 1
	for(i in seq(1,nrow(ss))) {
	  ss[i,weekIndex] <- sum(subset(mdwrain$HPCP, (mdwrain$DATE < (ss$collect.date.[i] + 24 * 60 * 60)) & (mdwrain$DATE > (ss$collect.date.[i] - 7 * 24 * 60 * 60))))
    }
    colnames(ss)[weekIndex] <- "rain1week"
    return(ss)
}	
attachRainForPastDays <- function(ss, mdwrain) {
	for(j in seq(0:7)) {
		index <- ncol(ss) + 1
		for(i in seq(1,nrow(ss))) {
			ss[i,index] <- sum(subset(mdwrain$HPCP, (mdwrain$DATE < (ss$collect.date.[i] - j * 24 * 60 * 60)) & (mdwrain$DATE > (ss$collect.date.[i] - (j+1) * 24 * 60 * 60))))
		}
		colnames(ss)[index] <- paste("day",j,"rain",sep="")
	}
    return(ss)	
}

runStatsFor <- function(sample_point) {
ss <- subset(wq, wq$sampling.point. == sample_point)
ss <- ss[c('fec_col.cts.100ml.','temp.deg_c.','collect.date.')]
ss <- addPreviousDaysTotalsAllLocations(ss, cso)
ss <- attachRainForPastWeek(ss, mdwrain)
ss <- attachRainForPastDays(ss, mdwrain)
ss <- subset(ss, ss$fec_col.cts.100ml. < 50000)
lmfit <- lm(ss$fec_col.cts.100ml. ~ .,data=ss[!names(ss) %in% c('collect.date.')])
summary(lmfit)
glmfit <- glm(ss$fec_col.cts.100ml. < 400 ~ ., data=ss[!names(ss) %in% c('collect.date.')],family = "binomial")
print(paste(glmfit$deviance,'-',glmfit$def.residual))
plot(ss[which(ss$fec_col.cts.100ml. < 10000),'fec_col.cts.100ml.'],predict(glmfit, newdata=ss[which(ss$fec_col.cts.100ml. < 10000),], type='response'))
abline(v=400)
abline(h=.5)
}

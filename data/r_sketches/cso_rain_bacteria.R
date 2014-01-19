wq <- read.csv('/Users/Thoughtworker/Programming/chicago-river-sewage/data/clean-waterway-measurements.csv')
wq$collect.date. <- strptime(wq$collect.date., format='%Y-%m-%d %H:%M:%S')
wq$fec_col.cts.100ml. <- as.integer(paste(wq$fec_col.cts.100ml.))
wq$fec_col.cts.100ml.[is.na(wq$fec_col.cts.100ml.)] <- 0 # Should we replace with zero?  Need to check the original intent of empty cells


cso <- read.csv("/Users/Thoughtworker/Programming/chicago-river-sewage/data/cso_events_timestamped.csv")
cso[4] <- data.frame(strptime(cso[,4], format="%Y-%m-%d %H:%M:%S %z"))
cso[3] <- data.frame(strptime(cso[,3], format="%Y-%m-%d %H:%M:%S %z"))
cso[5] <- cso[4] - cso[3]
colnames(cso)[3] <- "StartTime"
colnames(cso)[4] <- "EndTime"
colnames(cso)[5] <- "TotalOutflow"

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

sanship40 <- subset(wq, wq$sampling.point. == 'ww_40')
outfall_locations <- c('DS-M49','DS-M45','DS-M41','DS-M40','DS-M38','DS-M30','DS-M35','DS-M25')
sanship40 <- addPreviousWeekSums(sanship40, outfall_locations, cso)
sanship40 <- addPreviousDaysTotals(sanship40, outfall_locations, cso)





rainfall <- read.csv("/Users/Thoughtworker/Programming/chicago-river-sewage/data/hourly_rainfall_2005-2012.csv")
mdw_rain <- rainfall[rainfall$HPCP != 99999 & rainfall$STATION_NAME == "CHICAGO MIDWAY AIRPORT 3 SW IL US",c(6,7)]
mdw_rain[1] <- data.frame(strptime(mdw_rain[,1], format="%Y%m%d %H:%M"))
mdwrain <- aggregate(HPCP ~ DATE , data = mdw_rain, sum)

weekIndex <- ncol(sanship40) + 1
onedayindex <- weekIndex + 1
twodayindex <- weekIndex + 2
threedayindex <- weekIndex + 3	
for(i in seq(1,nrow(sanship40))) {
	sanship40[i,weekIndex] <- sum(subset(mdwrain$HPCP, (mdwrain$DATE < (sanship40$collect.date.[i] + 24 * 60 * 60)) & (mdwrain$DATE > (sanship40$collect.date.[i] - 7 * 24 * 60 * 60))))
	sanship40[i,onedayindex] <- sum(subset(mdwrain$HPCP, (mdwrain$DATE < (sanship40$collect.date.[i] + 24 * 60 * 60)) & (mdwrain$DATE > (sanship40$collect.date.[i] - 1 * 24 * 60 * 60))))
	sanship40[i,twodayindex] <- sum(subset(mdwrain$HPCP, (mdwrain$DATE < (sanship40$collect.date.[i] + 24 * 60 * 60)) & (mdwrain$DATE > (sanship40$collect.date.[i] - 2 * 24 * 60 * 60))))
	sanship40[i,threedayindex] <- sum(subset(mdwrain$HPCP, (mdwrain$DATE < (sanship40$collect.date.[i] + 24 * 60 * 60)) & (mdwrain$DATE > (sanship40$collect.date.[i] - 3 * 24 * 60 * 60))))
}	
colnames(sanship40)[weekIndex] <- "rain1week"
colnames(sanship40)[onedayindex] <- "rain1day"
colnames(sanship40)[twodayindex] <- "rain2days"
colnames(sanship40)[threedayindex] <- "rain3days"

ss <- subset(sanship40, sanship40$fec_col.cts.100ml. < 10000)
plot(as.integer(as.character(ss$temp.deg_c.)),ss$fec_col.cts.100ml.)
plot(format(ss$collect.date., format="%m"), ss$fec_col.cts.100ml.)


# Currently working on
#for(i in seq(0,7)){
#	vector()
#	for(j in outfall_locations) {
#		sum(ss[paste(i,'csoSum',i,'DaysBack',sep="_")]	
#	}
#}
#namesToSum <- append(namesToSum, paste(l))




#attach(sanship40)
#formula <- as.formula(paste("fec_col.cts.100ml. ~ ", paste(names(sanship40)[seq(5,ncol(sanship40))], collapse=" + "), sep=""))
#fit <- lm(formula, data=sanship40)


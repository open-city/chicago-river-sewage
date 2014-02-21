## =============================================================================
## 
##  File name:
##      testing.R
##   
##  Author: 
##      gong-yi@GongWS0
##
##  Created on:
##      2014/02/21 01:50:18
##   
##  Purpose:
##      To show 
##
##  Copyright:
##      BSD / Apache        
##
## -----------------------------------------------------------------------------

library(plyr)

sew.dat <- read.csv("../../data/processed_data/clean-waterway-measurements.csv",
                    stringsAsFactors=FALSE)

## get observations with "<"
get.df.nnumv <- function(x.df) {
    get.col.nnumv <- function(x) {
        y <- unique(x)
        return(y[is.na(as.numeric(y))])
    }
    return(apply(x.df, 2, get.col.nnumv))
}

##
sew.dat.numv <- sew.dat
sew.dat.numv$collect.date. <- NULL
sew.dat.numv$location.description <- NULL
sew.dat.problematic.vals <- get.df.nnumv(sew.dat)

## -----------------------------------------------------------------------------
##
##  End note:
##      (end note starts here)     
## 
## =============================================================================

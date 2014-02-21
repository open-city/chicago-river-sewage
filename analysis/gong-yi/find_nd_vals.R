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

## get observations with "<" or something non-numeric values 
get.df.nnumv <- function(x.df) {
    get.col.nnumv <- function(x) {
        y <- unique(x)
        return(y[is.na(as.numeric(y))])
    }
    return(apply(x.df, 2, get.col.nnumv))
}

## get lower bound values
get.lb.val <- function(x.col) {
    x.col.vals <- x.col
    x.col.is.lb <- rep(FALSE, length(x.col.vals))
    x.lb.idx <- grep("<", x.col.vals)
    x.col.is.lb[x.lb.idx]<- TRUE
    x.lb.vals <- x.col[x.lb.idx]
    x.lb.vals <- gsub("<", "", x.lb.vals)
    x.col.vals[x.lb.idx] <- x.lb.vals
    x.col.vals <- as.numeric(x.col.vals)
    x.col.annoted <- data.frame(x=x.col.vals,
                                 is.lb=x.col.is.lb)
    return(x.col.annoted)
}
    
## process numeric column first 
sew.dat.numv <- sew.dat

## these columns are already known to be non-numeric 
sew.dat.numv$collect.date. <- NULL
sew.dat.numv$location_description <- NULL

## get the problematic values in numeric columns, put them into a list 
sew.dat.problematic.vals <- get.df.nnumv(sew.dat.numv)




## -----------------------------------------------------------------------------
##
##  End note:
##      (end note starts here)     
## 
## =============================================================================

library(MASS)

water <- read.csv("clean-waterway-measurements.csv")
n_observations = dim(water)[1]

water_numeric <- apply(water, 2, function(x) as.numeric(as.character(x)))

missing <- apply(water_numeric, 2, function(x) sum(is.na(x))/n_observations)

water_numeric <- as.data.frame(water_numeric[, missing < 0.4])
water_numeric <- na.omit(water_numeric)

fit <- lm(log(fec_col.cts.100ml.) ~ ., data=water_numeric)

f <- MASS::stepAIC(fit)

print(attr(f$terms, "term.labels"))

actual <- resid(f) + fitted(f)

plot(exp(resid(f))/exp(actual) ~ actual,
     ylab="proportional error",
     xlab="fecal coliform, log scale")


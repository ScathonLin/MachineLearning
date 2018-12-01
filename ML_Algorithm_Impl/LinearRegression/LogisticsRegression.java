package com.huawei.scathon.aiops.logisticsregression;

import Jama.Matrix;

/**
 * @author linhuadong l00483433
 */
public interface LogisticRegression {
    /**
     * Start Logistics Regression Task
     *
     * @return theta result
     */
    Matrix startLogisticRegression();

    /**
     * loss function in LR which can use to validate LR whether run in a right way
     *  @param xMatrix feature matrix
     * @param yMatrix actual classfication value matrix
     * @param theta   args matrix
     */
    void loss(Matrix xMatrix, Matrix yMatrix, Matrix theta);

    /**
     * get init theta matrix
     *
     * @param featureCount the feature count of each sample item
     * @return init theta matrix(all items are one)
     */
    Matrix getThetaMatrix(int featureCount);

    /**
     * calculate and update theta matrix
     *
     * @param xMatrix feature matrix
     * @param yMatrix training sample classfication values
     * @param theta   θ matrix
     * @notes kernal step for logistic regression
     */
    void kernal(Matrix xMatrix, Matrix yMatrix, Matrix theta);

    /**
     * calculate sigmoid function value
     *
     * @param xMatrix feature matrix
     * @param yMatrix training sample classfication values
     * @param theta   theta matrix
     * @return sigmoid value for i.th sample
     */
    Matrix sigmoid(Matrix xMatrix, Matrix yMatrix, Matrix theta);

    /**
     * get actual classfication values in training set
     *
     * @param trainingSet training data set
     * @return y matrix
     */
    Matrix getY(Matrix trainingSet);

    /**
     * get training feature matrix
     *
     * @param trainingSet training data set
     * @return x matrix
     */
    Matrix getX(Matrix trainingSet);

    /**
     * read training data and package into jama matrix
     *
     * @return jama matrix
     */
    Matrix getOriginalDataMatrix();
}

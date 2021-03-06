/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 20.05.2005
 */
package net.finmath.marketdata.model.curves;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import net.finmath.marketdata.model.AnalyticModelInterface;

/**
 * Implementation of a discount factor curve given by a Nelson-Siegel-Svensson (NSS) parameterization.
 * In the NSS parameterization the zero rate \( r(T) \) is given by
 * 
 * \[ r(T) = \beta_0 + \beta_1 \frac{1-x_0}{T/\tau_0} + \beta_2 ( \frac{1-x_0}{T/\tau_0} - x_0) + \beta_3 ( \frac{1-x_1}{T/\tau_1} - x_1) \]
 * 
 * where \( x_0 = \exp(-T/\tau_0) \) and \( x_1 = \exp(-T/\tau_1) \).
 * 
 * The sub-family of curves with \( \beta_3 = 0 \) is called Nelson-Siegel parameterization.
 * 
 * Note: This is a time-parameterized model. The finmath lib library uses an internal mapping from date to times \( t \).
 * This mapping does not necessarily need to correspond with the curves understanding for the parameter \( T \).
 * For that reason this class allows to re-scale the time parameter. Currently only a simple re-scaling factor is
 * supported.
 * 
 * The parameter T used in the parameterization is given by <code>T = timeScaling * t</code>, where t is the maturity as an ACT/365
 * year fraction from the given reference date.
 * 
 * @author Christian Fries
 */
public class DiscountCurveNelsonSiegelSvensson extends AbstractCurve implements Serializable, DiscountCurveInterface {

	private static final long serialVersionUID = 8024640795839972709L;

	private final double	timeScaling;
	private final double[]	parameter;

	/**
	 * Create a discount curve using a Nelson-Siegel-Svensson parametrization.
	 * 
	 * @param name The name of the curve (the curve can be referenced under this name, if added to an <code>AnalyticModel</code>.
	 * @param referenceDate The reference date of this curve, i.e. the date associated with t=0.
	 * @param parameter The Nelson-Siegel-Svensson parameters in the order \( ( \beta_0, \beta_1, \beta_2, \beta_3, \tau_0, \tau_1 ) \).
	 * @param timeScaling The time parameter argument rescaling. See {@link #getDiscountFactor(AnalyticModelInterface, double)}.
	 */
	public DiscountCurveNelsonSiegelSvensson(String name, LocalDate referenceDate, double[] parameter, double timeScaling) {
		super(name, referenceDate);
		this.timeScaling = timeScaling;

		this.parameter = parameter.clone();
	}

	@Override
	public double getDiscountFactor(double maturity)
	{
		return getDiscountFactor(null, maturity);
	}

	/**
	 * Return the discount factor within a given model context for a given maturity.
	 * @param model The model used as a context (not required for this class).
	 * @param maturity The maturity in terms of ACT/365 daycount form this curve reference date. Note that this parameter might get rescaled to a different time parameter.
	 * @see net.finmath.marketdata.model.curves.DiscountCurveInterface#getDiscountFactor(net.finmath.marketdata.model.AnalyticModelInterface, double)
	 */
	@Override
	public double getDiscountFactor(AnalyticModelInterface model, double maturity)
	{
		// Change time scale
		maturity *= timeScaling;
		
		double beta1	= parameter[0];
		double beta2	= parameter[1];
		double beta3	= parameter[2];
		double beta4	= parameter[3];
		double tau1		= parameter[4];
		double tau2		= parameter[5];

		double x1 = tau1 > 0 ? FastMath.exp(-maturity/tau1) : 0.0;
		double x2 = tau2 > 0 ? FastMath.exp(-maturity/tau2) : 0.0;

		double y1 = tau1 > 0 ? (maturity > 0.0 ? (1.0-x1)/maturity*tau1 : 1.0) : 0.0;
		double y2 = tau2 > 0 ? (maturity > 0.0 ? (1.0-x2)/maturity*tau2 : 1.0) : 0.0;

		double zeroRate = beta1 + beta2 * y1 + beta3 * (y1-x1) + beta4 * (y2-x2);

		return Math.exp(- zeroRate * maturity);
	}

	@Override
	public double getValue(AnalyticModelInterface model, double time) {
		return getDiscountFactor(model, time);
	}

	@Override
	public CurveBuilderInterface getCloneBuilder() throws CloneNotSupportedException {
		return null;
	}

	@Override
	public double[] getParameter() {
		return parameter;
	}

	@Override
	@Deprecated
	public void setParameter(double[] parameter) {
		throw new UnsupportedOperationException();

	}

	public double getTimeScaling() {
		return timeScaling;
	}

	@Override
	public DiscountCurveNelsonSiegelSvensson clone() throws CloneNotSupportedException {
		return (DiscountCurveNelsonSiegelSvensson)super.clone();
	}

	@Override
	public DiscountCurveNelsonSiegelSvensson getCloneForParameter(double[] value) throws CloneNotSupportedException {
		return new DiscountCurveNelsonSiegelSvensson(getName(), getReferenceDate(), value, timeScaling);
	}

	@Override
	public String toString() {
		return "DiscountCurveNelsonSiegelSvensson [timeScaling=" + timeScaling
				+ ", parameter=" + Arrays.toString(parameter) + ", toString()="
				+ super.toString() + "]";
	}
}

package net.karmafiles.ff.core.tool.dbutil.daohelper;

/**
 * @author timur
 */
public interface EntityFilter<T> {

    boolean accepts(T candidate);
}

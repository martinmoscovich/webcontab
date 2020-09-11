package com.mmoscovich.webcontab.updater;

/**
 * Clase que representa un numero de version semantica
 * (major.minor.patch[-label]).
 * 
 * @see {@linkplain semver.org http://semver.org}
 */
public class SemVersion implements Comparable<SemVersion> {
    private Integer major;
    private Integer minor;
    private Integer patch;
    private String label;

    /**
     * Crea una instancia a partir de parsear el string
     * 
     * @param version
     * @throws IllegalArgumentException Si el string no es valido
     */
    public SemVersion(String version) throws IllegalArgumentException {
        String[] parts;

        if (version.contains("-")) {
            parts = version.split("-");
            version = parts[0];
            this.label = parts[1];
        }

        try {
            parts = version.split("\\.");
            if (parts.length >= 1) this.major = Integer.parseInt(parts[0]);
            if (parts.length >= 2) this.minor = Integer.parseInt(parts[1]);
            if (parts.length >= 3) this.patch = Integer.parseInt(parts[2]);
            if (this.label == null && parts.length >= 4) this.label = parts[3];
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La version '" + version + "' no es valida", e);
        }
    }
    
    /**
	 * @return <code>true</code> si tiene label. <code>true</code> en caso contrario
	 */
	public boolean hasLabel() {
		return this.label != null;
	}
	
	/**
	 * @return <code>true</code> si es una version SNAPSHOT (termina en "-SNAPSHOT").
	 */
	public boolean isSnapshot() {
		return "SNAPSHOT".equals(this.label);
	}

    private Long getValue() {
        int ma = this.major != null ? this.major : 0;
        int mi = this.minor != null ? this.minor : 0;
        int p = this.patch != null ? this.patch : 0;
        return (ma * 10000000000L + mi * 100000L + p);
    }
    
    @Override
	public int compareTo(SemVersion o) {
		if(o == null) return 1;
		
		int result = this.getValue().compareTo(o.getValue());
		
		if(result == 0) {
			if(this.hasLabel() && !o.hasLabel()) return -1;
			if(!this.hasLabel() && o.hasLabel()) return 1;
		}
		
		return result;
	}
    
    /**
	 * Compara y verifica si este Semver es mayor que otro
	 * @param other
	 * @return <code>true</code> si este semver es mayor, <code>false</code> en caso contrario.
	 */
	public boolean isGreaterThan(SemVersion other) {
		return this.compareTo(other) == 1;
	}
	
	/**
	 * Compara y verifica si este Semver es menor que otro
	 * @param other
	 * @return <code>true</code> si este semver es menor, <code>false</code> en caso contrario.
	 */
	public boolean isLessThan(SemVersion other) {
		return this.compareTo(other) == -1;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof SemVersion)) return false;

        SemVersion other = (SemVersion) obj;

        String tl = this.label == null ? "" : this.label;
        String ol = other.label == null ? "" : other.label;

        return (this.getValue().equals(other.getValue()) && tl.equals(ol));
    }

    public void increase() {
        if (this.patch != null) {
            this.patch++;
        } else if (this.minor != null) {
            this.minor++;
        } else if (this.major != null) {
            this.major++;
        }
    }
    
    public void decrease() {
        if (this.patch != null) {
            this.patch--;
        } else if (this.minor != null) {
            this.minor--;
        } else if (this.major != null) {
            this.major--;
        }
    }

    @Override
    public String toString() {
        String result = null;
        if (this.patch != null) {
            result = this.major + "." + this.minor + "." + this.patch;
        } else if (this.minor != null) {
            result = this.major + "." + this.minor;
        } else if (this.major != null) {
            result = this.major.toString();
        }
        if (result == null) return null;

        return result + (this.label != null ? "-" + this.label : "");
    }
}

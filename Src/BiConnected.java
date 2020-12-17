import java.util.HashSet;
import java.util.Vector;

class PairLR {
	String dataBaseName;
	int l, r;
	String left, right;
	public PairLR(int l, int r) {
		this.l = l;
		this.r = r;
	}
	
	public PairLR(int l, int r, String left,
				  String right) {
		this.l = l;
		this.r = r;
		this.left = left;
		this.right = right;

	}
	
	public PairLR(int l, int r, String left,
				  String right, String dataBaseName) {
		this.l = l;
		this.r = r;
		this.left = left;
		this.right = right;
		this.dataBaseName = dataBaseName;
	}

	@Override
	public String toString() {
		return"(l:"+l+", r:"+r+", left:"+left+", right:"+right+")";
	}
}

public class BiConnected {
	 int MAX_N = 1000000;

	
	Vector[] e;
	int dep[] = new int[MAX_N];
	Vector<Vector<PairLR>> ans = new Vector<Vector<PairLR>>();
	Vector<PairLR> Glo = new Vector<PairLR>();
	int n;

	public BiConnected(int s) {
		init(s);
	}
	public void init(int s)
	{
		n = s;
		MAX_N = n+10;
		e = new Vector[MAX_N];
		for (int i = 0; i < MAX_N; i++) {
			e[i] = new Vector<Integer>();
			e[i].clear();
			dep[i] = 0;
		}
		ans.clear();		
		
	}


	int dfs(int s, int t) {
		dep[s] = t;
		int Ans = t;
		int es = 0;
		for (int i = 0; i < e[s].size(); i++) {
			es = (Integer) e[s].get(i);
			if (dep[es] != 0) {
				Ans = Math.min(Ans, dep[es]);
				if (dep[es] < t)
					Glo.add(new PairLR(s, es));
			} else {
				int temp = Glo.size();
				int a = dfs(es, t + 1);
				if (a >= t) {
					ans.add(new Vector<PairLR>());
					while (Glo.size() > temp) {
						ans.lastElement().add(Glo.lastElement());
						Glo.removeElementAt(Glo.size() - 1);
					}
				}
				Ans = Math.min(Ans, a);
			}
		}
		return (Ans);
	}
		
	Vector<Vector<PairLR>> componentsOf(int s) {
		ans.clear();
		Glo.clear();
		for (int i = 0; i < MAX_N; i++) {
			dep[i] = 0;
		}
		for (int i = 1; i <= n; i++)
		{
			boolean isNeighbor = false;
			for(int j = 0; j < e[s].size(); j++)
				if((Integer)e[s].get(j) == i)
				{
					isNeighbor = true;
					break;
				}
			if ((isNeighbor || i == s) && dep[i] == 0)
			{
				dfs(i, 1);
			}
		}
		return (ans);
	}
	
	Vector<HashSet<Integer>> edgeToVertex(Vector<Vector<PairLR>> p)
	{
		Vector<HashSet<Integer>> v = new Vector<HashSet<Integer>>();
		HashSet<Integer> temp = new HashSet<Integer>();
		for(Vector<PairLR> v1 : p)
		{
			temp = new HashSet<Integer>();
			for(PairLR pair : v1)
			{
				temp.add(pair.l);
				temp.add(pair.r);
			}
			v.add(temp);
		}
		return v;
	}
	
}